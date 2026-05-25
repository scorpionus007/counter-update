import { Body, Controller, Post } from '@nestjs/common';
import { ApiCreatedResponse, ApiOperation, ApiTags } from '@nestjs/swagger';
import { PrismaService } from '../../prisma/prisma.service';
import { AuthService } from './auth.service';
import { RegisterDto, RegisterResponseDto } from './register.dto';

@ApiTags('Auth')
@Controller('auth')
export class AuthController {
  constructor(
    private readonly prisma: PrismaService,
    private readonly auth: AuthService,
  ) {}

  @Post('register')
  @ApiOperation({ summary: 'Register tester (name + phone + device)' })
  @ApiCreatedResponse({ type: RegisterResponseDto })
  async register(@Body() dto: RegisterDto): Promise<RegisterResponseDto> {
    const user = await this.prisma.user.upsert({
      where: { phone: dto.phone },
      create: {
        phone: dto.phone,
        name: dto.name,
        settings: { create: {} },
      },
      update: { name: dto.name },
    });

    const device = await this.prisma.device.upsert({
      where: {
        userId_androidDeviceId: {
          userId: user.id,
          androidDeviceId: dto.androidDeviceId,
        },
      },
      create: {
        userId: user.id,
        androidDeviceId: dto.androidDeviceId,
      },
      update: { lastSeenAt: new Date() },
    });

    const accessToken = this.auth.signToken(user.id, user.phone);

    return {
      userId: user.id,
      deviceId: device.id,
      accessToken,
      name: user.name,
      phone: user.phone,
    };
  }
}
